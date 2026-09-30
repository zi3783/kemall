local key = KEYS[1]
local s_key = KEYS[2]
local hashKey = ARGV[1]
local quantity = ARGV[2]
local price = ARGV[3]
local sku_id = ARGV[4]

local number = redis.call("HGET",key,hashKey)
local k = ( tonumber(number) or 0 ) + tonumber(quantity)
if k < 0 then
    return nil
end

redis.call("HSET", key, hashKey, k)

local is_select = false
if k == 0 then
    redis.call("SREM", s_key, sku_id)
else
    is_select = redis.call("SCARD",s_key)
end

local result = {
    total_price = redis.call("HINCRBY", key, "total:price", price),
    total_quantity = redis.call("HINCRBY", key, "total:quantity", quantity),
    selected = is_select
}

return cjson.encode(result)
