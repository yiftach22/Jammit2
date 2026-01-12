import { DataSource } from 'typeorm';
import { Instrument } from '../entities/instrument.entity';
import { getDatabaseConfig } from '../config/database.config';

async function seedInstruments() {
  const dataSource = new DataSource({
    ...getDatabaseConfig(),
    type: 'postgres',
  } as any);

  await dataSource.initialize();

  const instrumentRepository = dataSource.getRepository(Instrument);

  const instruments = [
    'Guitar',
    'Piano',
    'Drums',
    'Bass',
    'Violin',
    'Saxophone',
    'Trumpet',
    'Flute',
    'Voice',
    'Keyboard',
    'Cello',
    'Clarinet',
  ];

  for (const name of instruments) {
    const exists = await instrumentRepository.findOne({ where: { name } });
    if (!exists) {
      const instrument = instrumentRepository.create({ name });
      await instrumentRepository.save(instrument);
      console.log(`Created instrument: ${name}`);
    } else {
      console.log(`Instrument already exists: ${name}`);
    }
  }

  await dataSource.destroy();
  console.log('Seeding completed!');
}

seedInstruments().catch((error) => {
  console.error('Error seeding instruments:', error);
  process.exit(1);
});
