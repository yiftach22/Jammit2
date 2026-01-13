import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  OneToMany,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { InstrumentWithLevel } from './instrument-with-level.entity';
import { Chat } from './chat.entity';

@Entity('users')
export class User {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ unique: true })
  username: string;

  @Column({ unique: true, nullable: true })
  email: string;

  @Column({ nullable: true })
  password: string;

  @Column({ nullable: true, name: 'profile_picture_url' })
  profilePictureUrl: string;

  @Column('decimal', { precision: 10, scale: 8, nullable: true })
  latitude: number;

  @Column('decimal', { precision: 11, scale: 8, nullable: true })
  longitude: number;

  @OneToMany(() => InstrumentWithLevel, (instrument) => instrument.user, {
    cascade: true,
    eager: true,
  })
  instruments: InstrumentWithLevel[];

  @OneToMany(() => Chat, (chat) => chat.user1)
  chatsAsUser1: Chat[];

  @OneToMany(() => Chat, (chat) => chat.user2)
  chatsAsUser2: Chat[];

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;
}
