local key = KEYS[1]
local new_version = ARGV[1]
local new_data = ARGV[2]

local old_version = redis.call('hget', key, "version")

if not old_version or tonumber(old_version) < tonumber(new_version) then
    redis.call('hset', key, "version", new_version)
    redis.call('hset', key, "data", new_data)
    return true
else
    return false
end
