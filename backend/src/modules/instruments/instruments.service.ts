import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Instrument } from '../../entities/instrument.entity';

@Injectable()
export class InstrumentsService {
  constructor(
    @InjectRepository(Instrument)
    private instrumentRepository: Repository<Instrument>,
  ) {}

  async findAll(): Promise<Instrument[]> {
    return this.instrumentRepository.find();
  }

  async findOne(id: string): Promise<Instrument | null> {
    return this.instrumentRepository.findOne({ where: { id } });
  }

  async create(name: string): Promise<Instrument> {
    const instrument = this.instrumentRepository.create({ name });
    return this.instrumentRepository.save(instrument);
  }

  async seedInstruments(): Promise<void> {
    const instruments = [
      'Guitar',
      'Piano',
      'Drums',
      'Bass',
      'Violin',
      'Saxophone',
      'Trumpet',
      'Flute',
      'Voice',
      'Keyboard',
      'Cello',
      'Clarinet',
    ];

    for (const name of instruments) {
      const exists = await this.instrumentRepository.findOne({
        where: { name },
      });
      if (!exists) {
        await this.create(name);
      }
    }
  }
}
