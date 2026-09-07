local cart_key = KEYS[1]
local selected_key = KEYS[2]

redis.call('UNLINK', cart_key)
redis.call('UNLINK', selected_key)

return 1