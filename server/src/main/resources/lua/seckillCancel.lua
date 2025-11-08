redis.call("HINCRBY", KEYS[1], ARGV[1], 1)
redis.call("SREM", KEYS[2], ARGV[2])
return 0