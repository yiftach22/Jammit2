import { Controller, Get, Query, Param } from '@nestjs/common';
import { ExploreService } from './explore.service';
import { ExploreFiltersDto } from '../../dto/explore-filters.dto';
import { UserResponseDto } from '../../dto/user-response.dto';

@Controller('explore')
export class ExploreController {
  constructor(private readonly exploreService: ExploreService) {}

  @Get()
  async findNearbyUsers(
    @Query() filters: ExploreFiltersDto,
    @Query('currentUserId') currentUserId: string,
  ) {
    const usersWithDistance = await this.exploreService.findNearbyUsers(
      filters,
      currentUserId,
    );

    return usersWithDistance.map((item) => ({
      user: UserResponseDto.fromEntity(item.user),
      distance: item.distance,
    }));
  }
}
