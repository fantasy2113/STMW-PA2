CREATE TABLE IF NOT EXISTS `spatial_index`
(
    `item_id` INT(11) NOT NULL,
    `coord`   POINT NOT NULL,
    PRIMARY KEY (`item_id`),
    UNIQUE INDEX `item_id_UNIQUE` (`item_id` ASC),
    INDEX     `coord_idx` (`coord` ASC)
    ) ENGINE = MyISAM;

INSERT INTO spatial_index ( item_id, coord)
SELECT  item_coordinates.item_id, Point(item_coordinates.longitude, item_coordinates.latitude)
FROM    item_coordinates;