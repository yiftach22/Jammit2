import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../../entities/user.entity';
import { ExploreFiltersDto } from '../../dto/explore-filters.dto';
import { LocationUtil } from '../../utils/location.util';
import { MusicianLevel } from '../../entities/musician-level.enum';

export interface UserWithDistance {
  user: User;
  distance: number;
}

@Injectable()
export class ExploreService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  async findNearbyUsers(
    filters: ExploreFiltersDto,
    currentUserId: string,
  ): Promise<UserWithDistance[]> {
    // Get current user location
    const currentUser = await this.userRepository.findOne({
      where: { id: currentUserId },
    });

    // Use provided location or current user's location
    const searchLat = filters.latitude ?? currentUser?.latitude;
    const searchLon = filters.longitude ?? currentUser?.longitude;

    if (!searchLat || !searchLon) {
      return [];
    }

    // Get all users except current user
    let query = this.userRepository
      .createQueryBuilder('user')
      .leftJoinAndSelect('user.instruments', 'instruments')
      .leftJoinAndSelect('instruments.instrument', 'instrument')
      .where('user.id != :currentUserId', { currentUserId });

    // Filter by instruments
    if (filters.instrumentIds && filters.instrumentIds.length > 0) {
      query = query.andWhere('instruments.instrumentId IN (:...instrumentIds)', {
        instrumentIds: filters.instrumentIds,
      });
    }

    // Filter by level
    if (filters.level) {
      query = query.andWhere('instruments.level = :level', {
        level: filters.level,
      });
    }

    const users = await query.getMany();

    // Calculate distances and filter by radius
    const usersWithDistance = users
      .map((user) => {
        if (!user.latitude || !user.longitude) {
          return null;
        }

        const distance = LocationUtil.calculateDistance(
          searchLat,
          searchLon,
          user.latitude,
          user.longitude,
        );

        return {
          user,
          distance,
        };
      })
      .filter(
        (item): item is UserWithDistance =>
          item !== null &&
          (!filters.searchRadiusKm || item.distance <= filters.searchRadiusKm),
      )
      .sort((a, b) => a.distance - b.distance);

    return usersWithDistance;
  }
}
