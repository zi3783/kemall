-- Active: 1785938747882@@172.24.223.134@3306@product_db
create DATABASE product_db;

use product_db;

create table `category`(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类id',
    name VARCHAR(255) NOT NULL COMMENT '分类名称',
    parent_id BIGINT not null DEFAULT 0 COMMENT '父级分类id',
    `level` TINYINT not null COMMENT '分类层级',
    `path` VARCHAR(255) not null COMMENT '分类路径 如："/1/2/3/4"',
    sort_order INT not null DEFAULT 0 COMMENT  '排序序号',
    `status` TINYINT not null DEFAULT 1 COMMENT '分类状态 1:正常 2:禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '更新时间',
    -- index idx_path (path) todo
) COMMENT '商品分类表';

create table `brand`(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '品牌id',
    name VARCHAR(255) NOT NULL COMMENT '品牌名称',
    description VARCHAR(256) COMMENT '品牌描述',
    `status` TINYINT not null DEFAULT 1 COMMENT '品牌状态 1:正常 2:禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '更新时间'
)COMMENT '商品品牌表';


create table `product`(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品id',
    product_code VARCHAR(64) NOT NULL COMMENT '商品编码',
    name VARCHAR(255) NOT NULL COMMENT '商品名称',
    main_image VARCHAR(512) COMMENT '商品主图URL',
    `description` longtext COMMENT '商品描述',
    price BIGINT NOT NULL COMMENT '商品价格',

    category_id BIGINT NOT NULL COMMENT '商品末级分类id',
    brand_id BIGINT NOT NULL COMMENT '商品品牌id',

    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '商品状态 1:草稿 2:待审核 3:审核通过 4:上架 5:下架 6:违规下架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_product_code (product_code),
    KEY idx_category_id (category_id),
    KEY idx_brand_id (brand_id)
) COMMENT '商品表';

create table `product_sku`(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品SKU id',
    product_id BIGINT NOT NULL COMMENT '商品id',
    sku_code VARCHAR(64) NOT NULL COMMENT '商品SKU编码',

    price BIGINT NOT NULL COMMENT '商品价格 单位：分',
    main_image VARCHAR(512) COMMENT 'SKU主图',
    spec_json JSON NOT NULL COMMENT '规格值（如{"颜色":"黑色","容量":"128G"}）',

    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '商品SKU状态 1:正常 2:禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '更新时间',

    index idx_product_id (product_id),
    UNIQUE KEY uk_sku_code (sku_code)
)



select product.*, 
   brand.name as brand_name, 
   category.name as category_name,
   product_sku.price as sku_price, 
   product_sku.main_image as sku_image, 
   product_sku.spec_json as sku_json,
   product_sku.id as sku_id,
   product_sku.sku_code as sku_code,
   product_sku.product_id as product_id
from product
left join brand on product.brand_id = brand.id
left JOIN category on product.category_id = category.id
left join product_sku on product.id = product_sku.product_id
where product.id = 1
and product.status = 4
and product_sku.status = 1


select * 
from product
join category on product.category_id = category.id
WHERE category.path LIKE (
    select CONCAT(path, '/%')
    from category
    where id = 0
)
and product.status = 4
and category.status = 1
and brand_id = 1
