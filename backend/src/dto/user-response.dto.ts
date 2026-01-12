import { InstrumentWithLevel } from '../entities/instrument-with-level.entity';
import { User } from '../entities/user.entity';

export class InstrumentWithLevelResponseDto {
  id: string;
  instrument: {
    id: string;
    name: string;
  };
  level: string;
}

export class UserResponseDto {
  id: string;
  username: string;
  email?: string;
  profilePictureUrl?: string;
  instruments: InstrumentWithLevelResponseDto[];
  latitude?: number;
  longitude?: number;
  createdAt: Date;
  updatedAt: Date;

  static fromEntity(user: User): UserResponseDto {
    return {
      id: user.id,
      username: user.username,
      email: user.email,
      profilePictureUrl: user.profilePictureUrl,
      instruments: user.instruments.map((iwl) => ({
        id: iwl.id,
        instrument: {
          id: iwl.instrument.id,
          name: iwl.instrument.name,
        },
        level: iwl.level,
      })),
      latitude: user.latitude ? parseFloat(user.latitude.toString()) : undefined,
      longitude: user.longitude ? parseFloat(user.longitude.toString()) : undefined,
      createdAt: user.createdAt,
      updatedAt: user.updatedAt,
    };
  }
}
