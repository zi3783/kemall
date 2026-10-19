-- KEYS[1] 库存 key
-- KEYS[2] 已购用户 Set key
-- ARGV[1] userId

local stockKey = KEYS[1]
local usersKey = KEYS[2]
local userId   = ARGV[1]

-- 1. 库存 key 不存在：活动未初始化 / 已结束清理
if redis.call('EXISTS', stockKey) == 0 then
    return -1
end

local stock = tonumber(redis.call('GET', stockKey))
if stock == nil then
    return -1
end

-- 2. 先判重，保证已购用户始终返回“重复”而不是“售罄”
if redis.call('SISMEMBER', usersKey, userId) == 1 then
    return 2
end

-- 3. 库存不足
if stock <= 0 then
    return 1
end

-- 4. 原子扣减 + 记录用户
redis.call('DECR', stockKey)
redis.call('SADD', usersKey, userId)

return 0