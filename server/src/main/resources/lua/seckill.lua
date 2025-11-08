if redis.call("SISMEMBER", KEYS[1], ARGV[1]) == 1
    return 0
end
if redis.call("HGET", KEYS[2], ARGV[2]) > 0 then
    redis.call("SADD", KEYS[1], KEYS[1])
    return redis.call("HINCRBY", KEYS[2], ARGV[2], -1)
end
return 0