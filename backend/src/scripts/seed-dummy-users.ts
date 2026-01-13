import * as bcrypt from 'bcrypt';
import { AppDataSource } from '../config/data-source';
import { Instrument } from '../entities/instrument.entity';
import { InstrumentWithLevel } from '../entities/instrument-with-level.entity';
import { MusicianLevel } from '../entities/musician-level.enum';
import { User } from '../entities/user.entity';

function degToRad(deg: number) {
  return (deg * Math.PI) / 180;
}

function radToDeg(rad: number) {
  return (rad * 180) / Math.PI;
}

function randomInt(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pickRandomDistinct<T>(arr: T[], count: number): T[] {
  const copy = [...arr];
  const out: T[] = [];
  for (let i = 0; i < count && copy.length > 0; i++) {
    const idx = Math.floor(Math.random() * copy.length);
    out.push(copy[idx]);
    copy.splice(idx, 1);
  }
  return out;
}

/**
 * Random point within radius using spherical Earth approximation.
 * Radius input in km.
 */
function randomPointWithinKm(
  baseLat: number,
  baseLon: number,
  radiusKm: number,
): { lat: number; lon: number } {
  const earthRadiusKm = 6371;
  const radiusRad = radiusKm / earthRadiusKm;

  // Uniform over area: use sqrt(u)
  const u = Math.random();
  const v = Math.random();
  const w = radiusRad * Math.sqrt(u);
  const bearing = 2 * Math.PI * v;

  const lat1 = degToRad(baseLat);
  const lon1 = degToRad(baseLon);

  const lat2 = Math.asin(
    Math.sin(lat1) * Math.cos(w) +
      Math.cos(lat1) * Math.sin(w) * Math.cos(bearing),
  );
  const lon2 =
    lon1 +
    Math.atan2(
      Math.sin(bearing) * Math.sin(w) * Math.cos(lat1),
      Math.cos(w) - Math.sin(lat1) * Math.sin(lat2),
    );

  return { lat: radToDeg(lat2), lon: radToDeg(lon2) };
}

async function ensureInstruments() {
  const instrumentRepo = AppDataSource.getRepository(Instrument);
  const count = await instrumentRepo.count();
  if (count > 0) return;

  const names = [
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

  await instrumentRepo.save(names.map((name) => instrumentRepo.create({ name })));
}

async function seedDummyUsers() {
  await AppDataSource.initialize();

  await ensureInstruments();

  const userRepo = AppDataSource.getRepository(User);
  const instrumentRepo = AppDataSource.getRepository(Instrument);
  const iwlRepo = AppDataSource.getRepository(InstrumentWithLevel);

  const instruments = await instrumentRepo.find();
  if (instruments.length === 0) {
    throw new Error('No instruments found even after seeding.');
  }

  // Your target location:
  // 31°44'47.4"N 34°47'30.2"E => 31.7465, 34.791722...
  const baseLat = 31 + 44 / 60 + 47.4 / 3600;
  const baseLon = 34 + 47 / 60 + 30.2 / 3600;

  const numberOfUsers = 25;
  const passwordPlain = 'password123';
  const passwordHash = await bcrypt.hash(passwordPlain, 10);

  for (let i = 1; i <= numberOfUsers; i++) {
    const username = `dummy_user_${i}`;
    const email = `dummy_user_${i}@example.com`;

    // Skip if already exists
    const existing = await userRepo.findOne({ where: [{ username }, { email }] as any });
    if (existing) continue;

    const { lat, lon } = randomPointWithinKm(baseLat, baseLon, 10);

    const user = userRepo.create({
      username,
      email,
      password: passwordHash,
      latitude: lat,
      longitude: lon,
    });
    const saved = await userRepo.save(user);

    const instrumentCount = randomInt(1, 3);
    const picked = pickRandomDistinct(instruments, instrumentCount);

    const levels = Object.values(MusicianLevel);
    const links = picked.map((inst) =>
      iwlRepo.create({
        userId: saved.id,
        instrumentId: inst.id,
        level: levels[Math.floor(Math.random() * levels.length)] as MusicianLevel,
      }),
    );
    await iwlRepo.save(links);

    // eslint-disable-next-line no-console
    console.log(`Created ${username} with ${instrumentCount} instruments`);
  }

  await AppDataSource.destroy();
}

seedDummyUsers().catch((err) => {
  // eslint-disable-next-line no-console
  console.error(err);
  process.exit(1);
});

