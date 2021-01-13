use
ad;

CREATE TABLE IF NOT EXISTS spatial_index
(
    item_id INT
(
    11
) NOT NULL,
    coord POINT NOT NULL
    ) ENGINE = MyISAM;

CREATE
SPATIAL INDEX coord_index ON spatial_index (coord);

INSERT INTO spatial_index (item_id, coord)
SELECT item_coordinates.item_id, Point(item_coordinates.longitude, item_coordinates.latitude)
FROM item_coordinates;

CREATE TABLE has_category_idx
(
    item_id       INT(11) NOT NULL,
    category_name VARCHAR(128) NOT NULL,
    PRIMARY KEY (item_id, category_name)
);

INSERT INTO has_category_idx (item_id, category_name)
SELECT has_category.item_id, has_category.category_name
FROM has_category;