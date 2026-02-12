import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  OnGatewayConnection,
  OnGatewayDisconnect,
} from '@nestjs/websockets';
import { Server } from 'socket.io';
import { ChatsService } from './chats.service';
import { PushService } from '../users/push.service';
import { MessageResponseDto } from '../../dto/message-response.dto';

const CHAT_ROOM_PREFIX = 'chat:';
const USER_ROOM_PREFIX = 'user:';

@WebSocketGateway({
  cors: { origin: '*' },
  path: '/ws',
})
export class ChatGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server!: Server;

  constructor(
    private readonly chatsService: ChatsService,
    private readonly pushService: PushService,
  ) {}

  async handleConnection(client: {
    id: string;
    handshake: { query: Record<string, string | string[]> };
    join: (room: string) => void;
    emit: (event: string, data: unknown) => void;
  }) {
    const userId = Array.isArray(client.handshake.query?.userId)
      ? client.handshake.query.userId[0]
      : (client.handshake.query?.userId as string);
    const chatId = Array.isArray(client.handshake.query?.chatId)
      ? client.handshake.query.chatId[0]
      : (client.handshake.query?.chatId as string);

    if (!userId) {
      client.emit('error', { message: 'userId required in query' });
      return;
    }

    // Connection for notifications only (no chatId): join user room
    if (!chatId) {
      client.join(USER_ROOM_PREFIX + userId);
      return;
    }

    // Connection for a specific chat: verify access and join chat room
    try {
      await this.chatsService.findOne(chatId, userId);
    } catch {
      client.emit('error', { message: 'Chat not found or access denied' });
      return;
    }

    const room = CHAT_ROOM_PREFIX + chatId;
    client.join(room);
  }

  handleDisconnect() {
    // Socket leaves room automatically on disconnect
  }

  @SubscribeMessage('message')
  async handleMessage(
    client: {
      handshake: { query: Record<string, string | string[]> };
      emit: (event: string, data: unknown) => void;
    },
    payload: { content: string },
  ): Promise<void> {
    const userId = Array.isArray(client.handshake.query?.userId)
      ? client.handshake.query.userId[0]
      : (client.handshake.query?.userId as string);
    const chatId = Array.isArray(client.handshake.query?.chatId)
      ? client.handshake.query.chatId[0]
      : (client.handshake.query?.chatId as string);

    if (!userId || !chatId || !payload?.content?.trim()) {
      client.emit('error', { message: 'Invalid message payload' });
      return;
    }

    try {
      const message = await this.chatsService.createMessage(
        chatId,
        userId,
        payload.content.trim(),
      );

      const dto = MessageResponseDto.fromEntity({
        id: message.id,
        chatId: message.chatId,
        senderId: message.senderId,
        content: message.content,
        createdAt: message.createdAt,
      });

      const room = CHAT_ROOM_PREFIX + chatId;
      this.server.to(room).emit('message', dto);

      // Notify recipient for badge/notification (WebSocket when app is open, FCM when app is closed)
      const recipient = await this.chatsService.getRecipientForChat(chatId, userId);
      if (recipient) {
        this.server.to(USER_ROOM_PREFIX + recipient.recipientUserId).emit('new_message', {
          chatId,
          message: dto,
          senderUsername: recipient.senderUsername,
        });
        await this.pushService.sendChatMessageNotification(recipient.recipientUserId, {
          chatId,
          senderUsername: recipient.senderUsername,
          content: message.content,
        });
      }
    } catch (err) {
      client.emit('error', {
        message: err instanceof Error ? err.message : 'Failed to send message',
      });
    }
  }
}
