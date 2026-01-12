import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Chat } from '../../entities/chat.entity';
import { User } from '../../entities/user.entity';

@Injectable()
export class ChatsService {
  constructor(
    @InjectRepository(Chat)
    private chatRepository: Repository<Chat>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
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
}
