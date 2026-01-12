import { IsOptional, IsArray, IsString, IsNumber, Min, Max } from 'class-validator';
import { MusicianLevel } from '../entities/musician-level.enum';

export class ExploreFiltersDto {
  @IsArray()
  @IsString({ each: true })
  @IsOptional()
  instrumentIds?: string[];

  @IsString()
  @IsOptional()
  level?: MusicianLevel;

  @IsNumber()
  @Min(1)
  @Max(100)
  @IsOptional()
  searchRadiusKm?: number;

  @IsNumber()
  @IsOptional()
  latitude?: number;

  @IsNumber()
  @IsOptional()
  longitude?: number;
}
