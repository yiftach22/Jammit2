import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../../entities/user.entity';
import { InstrumentWithLevel } from '../../entities/instrument-with-level.entity';
import { Instrument } from '../../entities/instrument.entity';
import { UsersService } from './users.service';
import { UsersController } from './users.controller';
import { InstrumentsModule } from '../instruments/instruments.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([User, InstrumentWithLevel, Instrument]),
    InstrumentsModule,
  ],
  controllers: [UsersController],
  providers: [UsersService],
  exports: [UsersService],
})
export class UsersModule {}
