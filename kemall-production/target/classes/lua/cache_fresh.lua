local cacheKey = KEYS[1]
local versionKey = KEYS[2]
local new_data = ARGS[1]
local new_version = ARGS[2]
local ttl = ARGS[3]

local current_version = redis.call('get', versionKey)
if current_version == nil or tonumber(current_version) < tonumber(new_version) then
    redis.call('setex', cacheKey, ttl, new_data)
    redis.call('set', versionKey, new_version)
    return 1
end
return 0