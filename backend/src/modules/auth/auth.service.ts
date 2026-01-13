import {
  Injectable,
  UnauthorizedException,
  ConflictException,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { User } from '../../entities/user.entity';
import { InstrumentWithLevel } from '../../entities/instrument-with-level.entity';
import { InstrumentsService } from '../instruments/instruments.service';
import { MusicianLevel } from '../../entities/musician-level.enum';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
    @InjectRepository(InstrumentWithLevel)
    private instrumentWithLevelRepository: Repository<InstrumentWithLevel>,
    private jwtService: JwtService,
    private instrumentsService: InstrumentsService,
  ) {}

  async register(
    email: string,
    password: string,
  ): Promise<{ success: boolean }> {
    const trimmedEmail = (email || '').trim();
    if (!trimmedEmail) {
      throw new BadRequestException('Email is required');
    }

    // Check if user with email already exists
    const existingUser = await this.userRepository.findOne({
      where: { email: trimmedEmail },
    });
    
    if (existingUser) {
      throw new ConflictException('User with this email already exists');
    }

    // Just validate - don't create user yet
    // User will be created in completeRegistration
    return {
      success: true,
    };
  }

  async checkUsername(username: string): Promise<{ available: boolean }> {
    const trimmedUsername = (username || '').trim();
    if (!trimmedUsername) {
      return { available: false };
    }
    const existingUser = await this.userRepository.findOne({
      where: { username: trimmedUsername },
    });

    return {
      available: !existingUser,
    };
  }

  async completeRegistration(
    email: string,
    password: string,
    username: string,
    instruments: Array<{ instrumentId: string; level: string }>,
  ): Promise<{ success: boolean; token: string; userId: string }> {
    const trimmedEmail = (email || '').trim();
    const trimmedUsername = (username || '').trim();
    if (!trimmedEmail) {
      throw new BadRequestException('Email is required');
    }
    if (!trimmedUsername) {
      throw new BadRequestException('Username is required');
    }

    // Check if user with email already exists
    const existingUserByEmail = await this.userRepository.findOne({
      where: { email: trimmedEmail },
    });
    
    if (existingUserByEmail) {
      throw new ConflictException('User with this email already exists');
    }

    // Check if username is taken
    const existingUserByUsername = await this.userRepository.findOne({
      where: { username: trimmedUsername },
    });
    
    if (existingUserByUsername) {
      throw new ConflictException('Username already taken');
    }

    // Validate levels early (avoid DB enum failures => 500)
    const allowedLevels = Object.values(MusicianLevel);
    for (const iwl of instruments || []) {
      if (!allowedLevels.includes(iwl.level as MusicianLevel)) {
        throw new BadRequestException(`Invalid level: ${iwl.level}`);
      }
    }

    // Create user + instruments atomically (no partial user if something fails)
    return this.userRepository.manager.transaction(async (manager) => {
      const userRepo = manager.getRepository(User);
      const iwlRepo = manager.getRepository(InstrumentWithLevel);

      // Hash password
      const saltRounds = 10;
      const hashedPassword = await bcrypt.hash(password, saltRounds);

      // Create user
      const user = userRepo.create({
        email: trimmedEmail,
        password: hashedPassword,
        username: trimmedUsername,
      });

      const savedUser = await userRepo.save(user);

      // Create instrument associations if provided
      if (instruments && instruments.length > 0) {
        const instrumentsWithLevels = await Promise.all(
          instruments.map(async (iwl) => {
            const instrument = await this.instrumentsService.findOne(
              iwl.instrumentId,
            );
            if (!instrument) {
              throw new NotFoundException(
                `Instrument with ID ${iwl.instrumentId} not found`,
              );
            }

            return iwlRepo.create({
              userId: savedUser.id,
              instrumentId: iwl.instrumentId,
              level: iwl.level as MusicianLevel,
            });
          }),
        );

        await iwlRepo.save(instrumentsWithLevels);
      }

      // Generate JWT token
      const token = this.jwtService.sign({
        sub: savedUser.id,
        email: savedUser.email,
      });

      return {
        success: true,
        token,
        userId: savedUser.id,
      };
    });
  }

  async login(
    email: string,
    password: string,
  ): Promise<{ success: boolean; token: string; userId: string }> {
    const trimmedEmail = (email || '').trim();
    // Find user by email
    const user = await this.userRepository.findOne({
      where: { email: trimmedEmail },
    });

    if (!user) {
      throw new UnauthorizedException('Invalid email or password');
    }

    // Check if user has a password (for users created before password auth)
    if (!user.password) {
      throw new UnauthorizedException('Invalid email or password');
    }

    // Verify password
    const isPasswordValid = await bcrypt.compare(password, user.password);

    if (!isPasswordValid) {
      throw new UnauthorizedException('Invalid email or password');
    }

    // Generate JWT token
    const token = this.jwtService.sign({
      sub: user.id,
      email: user.email,
    });

    return {
      success: true,
      token,
      userId: user.id,
    };
  }

  async loginWithGoogle(token: string): Promise<{ success: boolean }> {
    // Google authentication not implemented
    throw new UnauthorizedException('Google authentication is not implemented');
  }
}
