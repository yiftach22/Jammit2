import { Controller, Post, Body, UsePipes, ValidationPipe, Get, Query } from '@nestjs/common';
import { AuthService } from './auth.service';
import {
  IsEmail,
  IsString,
  MinLength,
  IsArray,
  ValidateNested,
  IsOptional,
  IsEnum,
  IsUUID,
} from 'class-validator';
import { Type, Transform } from 'class-transformer';
import { MusicianLevel } from '../../entities/musician-level.enum';

export class LoginDto {
  @IsEmail()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  email: string;

  @IsString()
  @MinLength(6)
  password: string;
}

export class RegisterDto {
  @IsEmail()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  email: string;

  @IsString()
  @MinLength(6)
  password: string;
}

export class CheckUsernameDto {
  @IsString()
  username: string;
}

export class InstrumentWithLevelRegistrationDto {
  @IsUUID()
  instrumentId: string;

  @IsEnum(MusicianLevel)
  level: MusicianLevel;
}

export class CompleteRegistrationDto {
  @IsEmail()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  email: string;

  @IsString()
  @MinLength(6)
  password: string;

  @IsString()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  username: string;

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => InstrumentWithLevelRegistrationDto)
  @IsOptional()
  instruments?: InstrumentWithLevelRegistrationDto[];
}

export class GoogleLoginDto {
  @IsString()
  token: string;
}

@Controller('auth')
@UsePipes(new ValidationPipe({ whitelist: true, forbidNonWhitelisted: true }))
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('login')
  async login(@Body() loginDto: LoginDto) {
    return this.authService.login(loginDto.email, loginDto.password);
  }

  @Post('register')
  async register(@Body() registerDto: RegisterDto) {
    return this.authService.register(registerDto.email, registerDto.password);
  }

  @Get('check-username')
  async checkUsername(@Query('username') username: string) {
    return this.authService.checkUsername(username);
  }

  @Post('complete-registration')
  async completeRegistration(@Body() completeRegistrationDto: CompleteRegistrationDto) {
    return this.authService.completeRegistration(
      completeRegistrationDto.email,
      completeRegistrationDto.password,
      completeRegistrationDto.username,
      completeRegistrationDto.instruments || [],
    );
  }

  @Post('google')
  async loginWithGoogle(@Body() googleLoginDto: GoogleLoginDto) {
    return this.authService.loginWithGoogle(googleLoginDto.token);
  }
}
