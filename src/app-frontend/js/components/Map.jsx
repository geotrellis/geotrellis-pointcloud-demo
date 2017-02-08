import React, { Component, PropTypes } from 'react';
import { delay, isEqual } from 'lodash';
import L from 'leaflet';
import {
    Map as LeafletMap,
    TileLayer,
    Circle,
    CircleMarker,
    GeoJson,
} from 'react-leaflet';

import { GeoJSONGeometryTypeDef, LocationTypeDef } from 'TypeDefs';
import { metersPerMile } from 'constants';
import 'leaflet-fullscreen';

export default class Map extends Component {
    constructor() {
        super();

        this.adjustMap = this.adjustMap.bind(this);
    }

    componentDidMount() {
        const leafletMap = this.map.leafletElement;

        L.control.fullscreen({
            position: 'topleft',
            pseudoFullscreen: true,
        }).addTo(leafletMap);

        leafletMap.on('fullscreenchange', this.adjustMap);
    }

    componentWillReceiveProps({ center, radius }) {
        const hasNewCenter = !isEqual(center, this.props.center);
        const hasNewRadius = !isEqual(radius, this.props.radius);

        if (hasNewCenter || hasNewRadius) {
            this.adjustMap();
        }
    }

    adjustMap() {
        const { map, circleShape } = this;
        if (map && circleShape) {
            const { leafletElement: circle } = circleShape;
            const { leafletElement: leafletMap } = map;
            delay(() => { leafletMap.fitBounds(circle.getBounds()); }, 400);
        }
    }

    render() {
        const { center, zoom, radius, hoverGeom, highlightedGeom } = this.props;
        const geojsonMarkerOptions = {
            fillColor: '#1ABC9C',
            radius: 7,
            color: 'transparent',
            weight: 1,
            opacity: 1,
            fillOpacity: 1,
        };

        return (
            <LeafletMap
                center={[center.lat, center.lng]}
                zoom={zoom}
                ref={map => { this.map = map; }}
                animate
            >
                <TileLayer
                    url="http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                />
            </LeafletMap>
        );
    }
}

Map.propTypes = {
    center: LocationTypeDef.isRequired,
    radius: PropTypes.number.isRequired,
    zoom: PropTypes.number.isRequired,
    hoverGeom: GeoJSONGeometryTypeDef,
    highlightedGeom: GeoJSONGeometryTypeDef,
};
