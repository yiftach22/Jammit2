import { IsString, IsOptional, IsArray, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';
import { MusicianLevel } from '../entities/musician-level.enum';

export class InstrumentWithLevelDto {
  @IsString()
  instrumentId: string;

  @IsString()
  level: MusicianLevel;
}

export class UpdateUserDto {
  @IsString()
  @IsOptional()
  username?: string;

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => InstrumentWithLevelDto)
  @IsOptional()
  instruments?: InstrumentWithLevelDto[];

  @IsOptional()
  latitude?: number;

  @IsOptional()
  longitude?: number;
}
