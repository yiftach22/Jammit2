import { Chat } from '../entities/chat.entity';
import { UserResponseDto } from './user-response.dto';

export class ChatResponseDto {
  id: string;
  otherUser: UserResponseDto;
  lastMessage?: string;
  lastMessageTimestamp?: number;
  createdAt: Date;
  updatedAt: Date;

  static fromEntity(chat: Chat, currentUserId: string): ChatResponseDto {
    const otherUser = chat.user1Id === currentUserId ? chat.user2 : chat.user1;
    if (!otherUser) {
      throw new Error('Other user not found in chat');
    }
    return {
      id: chat.id,
      otherUser: UserResponseDto.fromEntity(otherUser),
      lastMessage: chat.lastMessage,
      lastMessageTimestamp: chat.lastMessageTimestamp,
      createdAt: chat.createdAt,
      updatedAt: chat.updatedAt,
    };
  }
}
