import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../../entities/user.entity';

let messaging: { send: (msg: unknown) => Promise<string> } | null = null;

function getMessaging() {
  if (messaging) return messaging;
  try {
    if (!process.env.GOOGLE_APPLICATION_CREDENTIALS && !process.env.FIREBASE_SERVICE_ACCOUNT_PATH) return null;
    const admin = require('firebase-admin');
    if (!admin.apps?.length) {
      admin.initializeApp();
    }
    messaging = admin.messaging();
    return messaging;
  } catch {
    return null;
  }
}

@Injectable()
export class PushService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  async sendChatMessageNotification(
    recipientUserId: string,
    payload: { chatId: string; senderUsername: string; content: string },
  ): Promise<void> {
    const user = await this.userRepository.findOne({
      where: { id: recipientUserId },
      select: ['id', 'fcmToken'],
    });
    if (!user?.fcmToken) return;

    const m = getMessaging();
    if (!m) return;

    const title = `New message from ${payload.senderUsername}`;
    const body = payload.content.length > 80 ? payload.content.slice(0, 77) + '...' : payload.content;

    try {
      await m.send({
        token: user.fcmToken,
        notification: { title, body },
        data: {
          type: 'chat_message',
          chatId: payload.chatId,
          senderUsername: payload.senderUsername,
          content: payload.content,
        },
        android: {
          priority: 'high',
          notification: { channelId: 'jammit_chat_messages' },
        },
      });
    } catch (err) {
      console.warn('FCM send failed:', err);
    }
  }
}
