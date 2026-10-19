-- KEYS[1] 库存 key
-- KEYS[2] 已购用户 Set key
-- ARGV[1] userId

if redis.call('SREM', KEYS[2], ARGV[1]) == 1 then
    if redis.call('EXISTS', KEYS[1]) == 1 then
        redis.call('INCR', KEYS[1])
    end
    return 1
end

return 0