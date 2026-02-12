export class MessageResponseDto {
  id: string;
  chatId: string;
  senderId: string;
  content: string;
  createdAt: Date;

  static fromEntity(msg: {
    id: string;
    chatId: string;
    senderId: string;
    content: string;
    createdAt: Date;
  }): MessageResponseDto {
    return {
      id: msg.id,
      chatId: msg.chatId,
      senderId: msg.senderId,
      content: msg.content,
      createdAt: msg.createdAt,
    };
  }
}
