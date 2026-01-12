import { IsString, IsEmail, IsOptional, IsArray, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';
import { MusicianLevel } from '../entities/musician-level.enum';

export class InstrumentWithLevelDto {
  @IsString()
  instrumentId: string;

  @IsString()
  level: MusicianLevel;
}

export class CreateUserDto {
  @IsString()
  username: string;

  @IsEmail()
  @IsOptional()
  email?: string;

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => InstrumentWithLevelDto)
  instruments: InstrumentWithLevelDto[];

  @IsOptional()
  latitude?: number;

  @IsOptional()
  longitude?: number;
}
