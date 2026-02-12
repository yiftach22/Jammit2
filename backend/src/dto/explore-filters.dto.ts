import {
  IsOptional,
  IsArray,
  IsString,
  IsNumber,
  Min,
  Max,
  IsUUID,
} from 'class-validator';
import { Type, Transform } from 'class-transformer';
import { MusicianLevel } from '../entities/musician-level.enum';

export class ExploreFiltersDto {
  // Included here because Nest's @Query() receives all query params,
  // and global ValidationPipe forbids unknown properties.
  @IsUUID()
  @IsOptional()
  currentUserId?: string;

  @Transform(({ value }) => {
    if (value == null) return undefined;
    if (Array.isArray(value)) return value;
    const s = String(value).trim();
    if (!s) return undefined;
    return s.includes(',')
      ? s.split(',').map((x) => x.trim()).filter(Boolean)
      : [s];
  })
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
  @Type(() => Number)
  @IsOptional()
  searchRadiusKm?: number;

  @IsNumber()
  @Type(() => Number)
  @IsOptional()
  latitude?: number;

  @IsNumber()
  @Type(() => Number)
  @IsOptional()
  longitude?: number;
}
