import { Entity, Column, PrimaryGeneratedColumn, OneToMany } from 'typeorm';
import { InstrumentWithLevel } from './instrument-with-level.entity';

@Entity('instruments')
export class Instrument {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ unique: true })
  name: string;

  @OneToMany(() => InstrumentWithLevel, (instrumentWithLevel) => instrumentWithLevel.instrument)
  instrumentWithLevels: InstrumentWithLevel[];
}
