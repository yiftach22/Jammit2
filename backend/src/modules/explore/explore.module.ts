import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from '../../entities/user.entity';
import { ExploreService } from './explore.service';
import { ExploreController } from './explore.controller';

@Module({
  imports: [TypeOrmModule.forFeature([User])],
  controllers: [ExploreController],
  providers: [ExploreService],
})
export class ExploreModule {}
