import Redis from 'ioredis';

const redisUrl = process.env.REDIS_URL ||
    `redis://${process.env.REDIS_HOST || 'localhost'}:${process.env.REDIS_PORT || 6379}`;

console.log(`Connecting to Redis at ${redisUrl}`);

export const redis = new Redis(redisUrl, {
    lazyConnect: true, // Conecta apenas na primeira chamada para não travar boot se redis cair
    retryStrategy(times) {
        const delay = Math.min(times * 50, 2000);
        return delay;
    },
});

redis.on('error', (err) => {
    console.warn('Redis connection error:', err.message);
});
