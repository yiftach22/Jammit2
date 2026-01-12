import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  ManyToOne,
  JoinColumn,
} from 'typeorm';
import { User } from './user.entity';
import { Instrument } from './instrument.entity';
import { MusicianLevel } from './musician-level.enum';

@Entity('instrument_with_levels')
export class InstrumentWithLevel {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => User, (user) => user.instruments, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'user_id' })
  user: User;

  @ManyToOne(() => Instrument, { eager: true })
  @JoinColumn({ name: 'instrument_id' })
  instrument: Instrument;

  @Column({
    type: 'enum',
    enum: MusicianLevel,
  })
  level: MusicianLevel;

  @Column({ name: 'user_id' })
  userId: string;

  @Column({ name: 'instrument_id' })
  instrumentId: string;
}
