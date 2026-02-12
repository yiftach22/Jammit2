import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Chat } from '../../entities/chat.entity';
import { User } from '../../entities/user.entity';
import { Message } from '../../entities/message.entity';

@Injectable()
export class ChatsService {
  constructor(
    @InjectRepository(Chat)
    private chatRepository: Repository<Chat>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
    @InjectRepository(Message)
    private messageRepository: Repository<Message>,
  ) {}

  async findAllForUser(userId: string): Promise<Chat[]> {
    return this.chatRepository.find({
      where: [{ user1Id: userId }, { user2Id: userId }],
      relations: ['user1', 'user2', 'user1.instruments', 'user1.instruments.instrument', 'user2.instruments', 'user2.instruments.instrument'],
      order: { lastMessageTimestamp: 'DESC' },
    });
  }

  async findOrCreateChat(user1Id: string, user2Id: string): Promise<Chat> {
    // Check if chat already exists
    let chat = await this.chatRepository.findOne({
      where: [
        { user1Id, user2Id },
        { user1Id: user2Id, user2Id: user1Id },
      ],
      relations: ['user1', 'user2'],
    });

    if (!chat) {
      // Verify both users exist
      const user1 = await this.userRepository.findOne({ where: { id: user1Id } });
      const user2 = await this.userRepository.findOne({ where: { id: user2Id } });

      if (!user1 || !user2) {
        throw new NotFoundException('One or both users not found');
      }

      // Create new chat
      chat = this.chatRepository.create({
        user1Id,
        user2Id,
      });

      chat = await this.chatRepository.save(chat);
    }

    const foundChat = await this.chatRepository.findOne({
      where: { id: chat.id },
      relations: ['user1', 'user2', 'user1.instruments', 'user1.instruments.instrument', 'user2.instruments', 'user2.instruments.instrument'],
    });

    if (!foundChat) {
      throw new NotFoundException('Chat not found after creation');
    }

    return foundChat;
  }

  async findOne(id: string, userId: string): Promise<Chat> {
    const chat = await this.chatRepository.findOne({
      where: [
        { id, user1Id: userId },
        { id, user2Id: userId },
      ],
      relations: ['user1', 'user2', 'user1.instruments', 'user1.instruments.instrument', 'user2.instruments', 'user2.instruments.instrument'],
    });

    if (!chat) {
      throw new NotFoundException('Chat not found or access denied');
    }

    return chat;
  }

  /** Returns recipient userId and sender username for new_message notification. */
  async getRecipientForChat(chatId: string, senderId: string): Promise<{ recipientUserId: string; senderUsername: string } | null> {
    const chat = await this.chatRepository.findOne({
      where: { id: chatId },
      relations: ['user1', 'user2'],
    });
    if (!chat) return null;
    const recipient = chat.user1Id === senderId ? chat.user2 : chat.user1;
    const sender = chat.user1Id === senderId ? chat.user1 : chat.user2;
    return recipient && sender ? { recipientUserId: recipient.id, senderUsername: sender.username } : null;
  }

  async getMessages(chatId: string, userId: string): Promise<Message[]> {
    const chat = await this.findOne(chatId, userId);
    if (!chat) return [];

    return this.messageRepository.find({
      where: { chatId: chat.id },
      order: { createdAt: 'ASC' },
    });
  }

  async createMessage(chatId: string, senderId: string, content: string): Promise<Message> {
    const chat = await this.chatRepository.findOne({
      where: [
        { id: chatId, user1Id: senderId },
        { id: chatId, user2Id: senderId },
      ],
    });
    if (!chat) {
      throw new NotFoundException('Chat not found or access denied');
    }

    const message = this.messageRepository.create({
      chatId,
      senderId,
      content: content.trim(),
    });
    const saved = await this.messageRepository.save(message);

    const now = Date.now();
    await this.chatRepository.update(chat.id, {
      lastMessage: saved.content.substring(0, 200),
      lastMessageTimestamp: now,
    });

    return saved;
  }
}
