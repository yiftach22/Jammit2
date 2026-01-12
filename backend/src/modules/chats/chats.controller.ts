import { Controller, Get, Post, Param, Body } from '@nestjs/common';
import { ChatsService } from './chats.service';
import { ChatResponseDto } from '../../dto/chat-response.dto';

@Controller('chats')
export class ChatsController {
  constructor(private readonly chatsService: ChatsService) {}

  @Get(':userId')
  async findAllForUser(@Param('userId') userId: string): Promise<ChatResponseDto[]> {
    const chats = await this.chatsService.findAllForUser(userId);
    return chats.map((chat) => ChatResponseDto.fromEntity(chat, userId));
  }

  @Get(':userId/:chatId')
  async findOne(
    @Param('userId') userId: string,
    @Param('chatId') chatId: string,
  ): Promise<ChatResponseDto> {
    const chat = await this.chatsService.findOne(chatId, userId);
    return ChatResponseDto.fromEntity(chat, userId);
  }

  @Post()
  async findOrCreateChat(
    @Body() body: { user1Id: string; user2Id: string },
  ): Promise<ChatResponseDto> {
    const chat = await this.chatsService.findOrCreateChat(
      body.user1Id,
      body.user2Id,
    );
    return ChatResponseDto.fromEntity(chat, body.user1Id);
  }
}
