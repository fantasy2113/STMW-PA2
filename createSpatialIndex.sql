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

ALTER TABLE `has_category`
    CHANGE COLUMN `item_id` `item_id` INT(11) NOT NULL ,
    CHANGE COLUMN `category_name` `category_name` VARCHAR(128) NOT NULL;

ALTER TABLE `has_category`
    ADD PRIMARY KEY (`item_id`, `category_name`);