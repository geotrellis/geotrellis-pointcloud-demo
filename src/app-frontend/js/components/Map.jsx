import React, { Component, PropTypes } from 'react';
import { delay, isEqual } from 'lodash';
import L from 'leaflet';
import {
    Map as LeafletMap,
    TileLayer,
    GeoJson,
    ZoomControl
} from 'react-leaflet';

import { GeoJSONGeometryTypeDef, LocationTypeDef } from 'TypeDefs';
import { metersPerMile } from 'constants';
import 'leaflet-fullscreen';

import DrawToolbar from './DrawToolbar';

export default class Map extends Component {
    constructor() {
        super();

        this.adjustMap = this.adjustMap.bind(this);
        this.createDrawToolbarComponent = this.createDrawToolbarComponent.bind(this);
    }

    componentDidMount() {
        /* const leafletMap = this.map.leafletElement;

         * L.control.fullscreen({
         *     position: 'topLeft',
         *     pseudoFullscreen: true,
         * }).addTo(leafletMap);

         * leafletMap.on('fullscreenchange', this.adjustMap);*/
    }

    componentWillReceiveProps({ center }) {
        const hasNewCenter = !isEqual(center, this.props.center);

        if (hasNewCenter) {
            this.adjustMap();
        }
    }

    adjustMap() {
        const { map } = this;
        if (map) {
            // TODO: Center map.
            /* const { leafletElement: circle } = circleShape;
             * const { leafletElement: leafletMap } = map;
             * delay(() => { leafletMap.fitBounds(circle.getBounds()); }, 400);*/
        }
    }

    createDrawToolbarComponent() {
        if (this.props.analysisOn) {
            return (
                <DrawToolbar
                    onClearGeometries={this.props.onClearGeometries}
                    onSetPolygon={this.props.onSetPolygon}
                    onSetPoint={this.props.onSetPoint}
                    isClear={(!this.props.polygon) && (!this.props.point)}
                />
            );
        }

        return null;
    }

    render() {
        const { targetLayerOpacity,
                center,
                zoom,
                targetLayerName,
                renderMethod,
                polygon,
                point } = this.props;

        console.log("IN MAP: " + targetLayerName)
        var layer = targetLayerName == "SNOW-ON" ? "mar10idw" : "jul10idw"
        var colorRamp = "blue-to-red"

        var hostname = window.location.hostname

        /* var demLayerUrl = 'http://' + hostname + ':7070/tms/png/{layer}/{z}/{x}/{y}?colorRamp={colorRamp}'*/
        console.log(demLayerUrl)
        console.log(layer)

        let targetLayerPath = targetLayerName == "SNOW-ON" ? "mar10idw" : "jul10idw";

        let renderMethodPath = renderMethod == "COLORRAMP" ? "png" : "hillshade-buffered";

        let demLayerUrl = 'http://' + hostname + ':7070/tms/' + renderMethodPath + '/' + targetLayerPath + '/{z}/{x}/{y}?colorRamp={colorRamp}'

        let targetLayer = [<TileLayer
                               key="targetLayer"
                               url={demLayerUrl}
                               colorRamp={colorRamp}
                               opacity={targetLayerOpacity}
                               maxZoom={19}
                           />]


        const drawToolbar = this.createDrawToolbarComponent();

        return (
            <LeafletMap
                center={[center.lat, center.lng]}
                zoom={zoom}
                ref={map => { this.map = map; }}
                animate
            >
                <ZoomControl position="topright" />
                <TileLayer
                    url="http://tile.stamen.com/terrain-background/{z}/{x}/{y}@2x.jpg"
                />

                {/* <TileLayer
                url="http://c.tiles.mapbox.com/v3/mapbox.world-light/{z}/{x}/{y}.png"
                /> */}

                <TileLayer
                    url="http://tile.stamen.com/toner-labels/{z}/{x}/{y}@2x.png"
                />

                {targetLayer}
                {drawToolbar}
            </LeafletMap>
        );
    }
}

Map.propTypes = {
    center: LocationTypeDef.isRequired,
    zoom: PropTypes.number.isRequired,
    targetLayerName: PropTypes.string.isRequired,
    renderMethod: PropTypes.string.isRequired,
    analysisOn: PropTypes.bool.isRequired,
    onClearGeometries: PropTypes.func.isRequired,
    onSetPolygon: PropTypes.func.isRequired,
    onSetPoint: PropTypes.func.isRequired,
};
