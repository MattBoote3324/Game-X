def normalise(xml):
    """
    Normalise a race xml so that all latitudes and longitudes fit in the range (-desired_max,desired_max).
    desired_max of 0.015 will make the course fit in a ~ 1668m * 1668m bounding box,
    slightly larger than the original Gothenburg course.
    """

    desired_max = 0.015

    # Replace all single quotes with double quotes, just in case.
    cleaned_xml = ""
    for c in xml:
        if c == "'":
            c = '"'
        cleaned_xml += c
    xml = cleaned_xml

    # Find range of latitudes.
    smallest_latitude, largest_latitude = find_range_of_attribute(xml, "lat")
    latitude_range = largest_latitude - smallest_latitude

    # Find range of longitudes
    smallest_lng = float("inf")
    smallest_lon = float("inf")
    largest_lng = -float("inf")
    largest_lon = -float("inf")
    for line in xml.splitlines():
        smallest_lng = check_new_extreme_value(line, "lng", smallest_lng, lambda value, current: value < current)
        smallest_lon = check_new_extreme_value(line, "lon", smallest_lon, lambda value, current: value < current)
        largest_lng = check_new_extreme_value(line, "lng", largest_lng, lambda value, current: value > current)
        largest_lon = check_new_extreme_value(line, "lon", largest_lon, lambda value, current: value > current)
    smallest_longitude = min(smallest_lng, smallest_lon)
    largest_longitude = max(largest_lng, largest_lon)
    longitude_range = largest_longitude - smallest_longitude

    largest_range = max(latitude_range, longitude_range)

    scale_factor = desired_max * 2 / largest_range

    center_lat = largest_latitude - latitude_range / 2
    center_lon = largest_longitude - longitude_range / 2

    # Build the scaled xml, replacing all latitudes and longitudes with scaled versions
    transformed_xml = transform_xml_values(xml, "lat", scale_factor, center_lat)
    transformed_xml = transform_xml_values(transformed_xml, "lon", scale_factor, center_lon)
    transformed_xml = transform_xml_values(transformed_xml, "lng", scale_factor, center_lon)

    print(transformed_xml)


def find_range_of_attribute(xml, attribute):
    largest = -float("inf")
    smallest = float("inf")
    for line in xml.splitlines():
        largest = check_new_extreme_value(line, attribute, largest, lambda value, current: value > current)
        smallest = check_new_extreme_value(line, attribute, smallest, lambda value, current: value < current)
    return smallest, largest


def check_new_extreme_value(line, attribute, current_extreme, comparator):
    value = get_value_from_line(line, attribute)
    if value and comparator(float(value), current_extreme):
        return float(value)
    else:
        return current_extreme


def transform_xml_values(xml, attribute, scale_factor, current_center_attribute):
    transformed_xml = ""
    for line in xml.splitlines(True):
        index_range = get_index_range_of_value_form_line(line, attribute)
        if index_range:
            start, stop = index_range
            value = float(line[start:stop])
            offset_from_current_center = value - current_center_attribute
            transformed_value = offset_from_current_center * scale_factor
            transformed_line = line[:start] + str(transformed_value) + line[stop:]
            transformed_xml += transformed_line
        else:
            transformed_xml += line
    return transformed_xml


def get_value_from_line(line, attribute):
    """ Ignores case """
    index_range = get_index_range_of_value_form_line(line, attribute)
    if index_range:
        start, stop = index_range
        attribute = line[start:stop]
        return attribute
    else:
        return False


def get_index_range_of_value_form_line(line, attribute):
    """ Returns (start, stop]"""
    if attribute in line.lower():
        attribute_index = line.lower().find(attribute)
        open_quote_index = line[attribute_index:].lower().find('"') + attribute_index
        close_quote_index = line[open_quote_index + 1:].lower().find('"') + open_quote_index + 1
        return open_quote_index + 1, close_quote_index
    else:
        return False



race_xml = """
<?xml version="1.0" encoding="utf-8"?>
<Race>
  <CreationTimeDate>2016-09-10T12:51:42+02:00</CreationTimeDate>
  <RaceStartTime Start="2016-09-10T14:37:00+02:00" Postpone="False" />
  <RaceID>16091001</RaceID>
  <RaceType>Fleet</RaceType>
  <Participants>
    <Yacht SourceID="101" />
    <Yacht SourceID="102" Entry="Stbd" />
    <Yacht SourceID="103" />
    <Yacht SourceID="104" Entry="Port" />
    <Yacht SourceID="105" />
    <Yacht SourceID="106" />
  </Participants>
  <Course>
    <CompoundMark CompoundMarkID="1" Name="StartLine">
      <Mark SeqID="1" Name="StartLine1" TargetLat="43.1003020" TargetLng="5.9462540" SourceID="122" />
      <Mark SeqID="2" Name="StartLine2" TargetLat="43.1015750" TargetLng="5.9467030" SourceID="123" />
    </CompoundMark>
    <CompoundMark CompoundMarkID="2" Name="Mark1">
      <Mark SeqID="1" Name="Mark1" TargetLat="43.1007020" TargetLng="5.9524970" SourceID="126" />
    </CompoundMark>
    <CompoundMark CompoundMarkID="3" Name="Gate1">
      <Mark SeqID="1" Name="Gate1_1" TargetLat="43.1051880" TargetLng="5.9514100" SourceID="124" />
      <Mark SeqID="2" Name="Gate1_2" TargetLat="43.1053390" TargetLng="5.9528740" SourceID="125" />
    </CompoundMark>
    <CompoundMark CompoundMarkID="4" Name="Gate2">
      <Mark SeqID="1" Name="Gate2_1" TargetLat="43.0973570" TargetLng="5.9540870" SourceID="131" />
      <Mark SeqID="2" Name="Gate2_2" TargetLat="43.0975470" TargetLng="5.9553000" SourceID="127" />
    </CompoundMark>
    <CompoundMark CompoundMarkID="5" Name="FinishLine">
      <Mark SeqID="1" Name="FinishLine1" TargetLat="43.1055490" TargetLng="5.9546350" SourceID="128" />
      <Mark SeqID="2" Name="FinishLine2" TargetLat="43.1048740" TargetLng="5.9545000" SourceID="129" />
    </CompoundMark>
  </Course>
  <CompoundMarkSequence>
    <Corner SeqID="1" CompoundMarkID="1" Rounding="SP" ZoneSize="3" />
    <Corner SeqID="2" CompoundMarkID="2" Rounding="Port" ZoneSize="3" />
    <Corner SeqID="3" CompoundMarkID="3" Rounding="SP" ZoneSize="3" />
    <Corner SeqID="4" CompoundMarkID="4" Rounding="PS" ZoneSize="3" />
    <Corner SeqID="5" CompoundMarkID="5" Rounding="SP" ZoneSize="3" />
  </CompoundMarkSequence>
  <CourseLimit name="Boundary" draw="1" avoid="1" fill="1" lock="0" colour="000000FF" notes="5,45">
    <Limit SeqID="1" Lat="43.1054570" Lon="5.9480230" />
    <Limit SeqID="2" Lat="43.1059690" Lon="5.9515090" />
    <Limit SeqID="3" Lat="43.1059890" Lon="5.9529550" />
    <Limit SeqID="4" Lat="43.1055820" Lon="5.9555870" />
    <Limit SeqID="5" Lat="43.0970690" Lon="5.9583720" />
    <Limit SeqID="6" Lat="43.0961770" Lon="5.9553530" />
    <Limit SeqID="7" Lat="43.0957830" Lon="5.9515720" />
    <Limit SeqID="8" Lat="43.0960650" Lon="5.9502690" />
    <Limit SeqID="9" Lat="43.0992660" Lon="5.9494250" />
    <Limit SeqID="10" Lat="43.0988260" Lon="5.9434330" />
    <Limit SeqID="11" Lat="43.1024400" Lon="5.9429390" />
    <Limit SeqID="12" Lat="43.1028210" Lon="5.9487690" />
  </CourseLimit>
</Race>
"""

normalise(race_xml)