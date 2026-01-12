import { Controller, Get, Param, NotFoundException } from '@nestjs/common';
import { InstrumentsService } from './instruments.service';

@Controller('instruments')
export class InstrumentsController {
  constructor(private readonly instrumentsService: InstrumentsService) {}

  @Get()
  async findAll() {
    return this.instrumentsService.findAll();
  }

  @Get(':id')
  async findOne(@Param('id') id: string) {
    const instrument = await this.instrumentsService.findOne(id);
    if (!instrument) {
      throw new NotFoundException(`Instrument with ID ${id} not found`);
    }
    return instrument;
  }
}
