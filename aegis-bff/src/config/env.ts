import { z } from 'zod';
import dotenv from 'dotenv';

dotenv.config();

const envSchema = z.object({
  PORT: z.string().default('3000'),
  BACKEND_URL: z.string().url().default('http://localhost:8080'),
  USE_MOCKS: z.string().transform((val) => val === 'true').default('true'), // Feature flag para mocks
});

export const env = envSchema.parse(process.env);
