CREATE TABLE IF NOT EXISTS ad.spatial_index
(
    item_id INT
(
    11
) NOT NULL,
    coord POINT NOT NULL
    ) engine = myisam;

CREATE
SPATIAL INDEX coord_index ON ad.spatial_index (coord);

INSERT INTO ad.spatial_index
    (item_id, coord)
SELECT ad.item_coordinates.item_id,
       Point(ad.item_coordinates.longitude, ad.item_coordinates.latitude)
FROM ad.item_coordinates;

CREATE TABLE IF NOT EXISTS ad.has_category_idx
(
    item_id INT
(
    11
) NOT NULL,
    category_name VARCHAR
(
    128
) NOT NULL,
    PRIMARY KEY
(
    item_id,
    category_name
)
    );

INSERT INTO ad.has_category_idx
    (item_id, category_name)
SELECT ad.has_category.item_id,
       ad.has_category.category_name
FROM ad.has_category;