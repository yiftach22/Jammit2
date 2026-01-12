import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../../entities/user.entity';
import { InstrumentWithLevel } from '../../entities/instrument-with-level.entity';
import { Instrument } from '../../entities/instrument.entity';
import { CreateUserDto } from '../../dto/create-user.dto';
import { UpdateUserDto } from '../../dto/update-user.dto';
import { InstrumentsService } from '../instruments/instruments.service';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
    @InjectRepository(InstrumentWithLevel)
    private instrumentWithLevelRepository: Repository<InstrumentWithLevel>,
    private instrumentsService: InstrumentsService,
  ) {}

  async create(createUserDto: CreateUserDto): Promise<User> {
    const user = this.userRepository.create({
      username: createUserDto.username,
      email: createUserDto.email,
      latitude: createUserDto.latitude,
      longitude: createUserDto.longitude,
    });

    const savedUser = await this.userRepository.save(user);

    // Create instrument with level associations
    const instrumentsWithLevels = await Promise.all(
      createUserDto.instruments.map(async (iwlDto) => {
        const instrument = await this.instrumentsService.findOne(
          iwlDto.instrumentId,
        );
        if (!instrument) {
          throw new NotFoundException(
            `Instrument with ID ${iwlDto.instrumentId} not found`,
          );
        }

        return this.instrumentWithLevelRepository.create({
          userId: savedUser.id,
          instrumentId: iwlDto.instrumentId,
          level: iwlDto.level,
        });
      }),
    );

    await this.instrumentWithLevelRepository.save(instrumentsWithLevels);

    return this.findOne(savedUser.id);
  }

  async findAll(): Promise<User[]> {
    return this.userRepository.find({
      relations: ['instruments', 'instruments.instrument'],
    });
  }

  async findOne(id: string): Promise<User> {
    const user = await this.userRepository.findOne({
      where: { id },
      relations: ['instruments', 'instruments.instrument'],
    });

    if (!user) {
      throw new NotFoundException(`User with ID ${id} not found`);
    }

    return user;
  }

  async update(id: string, updateUserDto: UpdateUserDto): Promise<User> {
    const user = await this.findOne(id);

    if (updateUserDto.username) {
      user.username = updateUserDto.username;
    }
    if (updateUserDto.latitude !== undefined) {
      user.latitude = updateUserDto.latitude;
    }
    if (updateUserDto.longitude !== undefined) {
      user.longitude = updateUserDto.longitude;
    }

    await this.userRepository.save(user);

    // Update instruments if provided
    if (updateUserDto.instruments) {
      // Remove existing instruments
      await this.instrumentWithLevelRepository.delete({ userId: id });

      // Create new instruments
      const instrumentsWithLevels = await Promise.all(
        updateUserDto.instruments.map(async (iwlDto) => {
          const instrument = await this.instrumentsService.findOne(
            iwlDto.instrumentId,
          );
          if (!instrument) {
            throw new NotFoundException(
              `Instrument with ID ${iwlDto.instrumentId} not found`,
            );
          }

          return this.instrumentWithLevelRepository.create({
            userId: id,
            instrumentId: iwlDto.instrumentId,
            level: iwlDto.level,
          });
        }),
      );

      await this.instrumentWithLevelRepository.save(instrumentsWithLevels);
    }

    return this.findOne(id);
  }

  async remove(id: string): Promise<void> {
    const user = await this.findOne(id);
    await this.userRepository.remove(user);
  }
}
