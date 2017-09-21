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

    render() {
        const { targetLayerOpacity,
                center,
                zoom,
                targetLayerName,
                renderMethod } = this.props;

        console.log("IN MAP: " + targetLayerName)
        var layer = targetLayerName == "SNOW-ON" ? "mar10idw" : "jul10idw"
        var colorRamp = "blue-to-red"

        var hostname = window.location.hostname

        var tms_endpoint = hostname == 'localhost' ? 'http://localhost:8000/gt/tms/' : 'https://' + hostname + '/gt/tms/';
        
        /* var demLayerUrl = 'http://' + hostname + ':7070/tms/png/{layer}/{z}/{x}/{y}?colorRamp={colorRamp}'*/
        console.log(demLayerUrl)
        console.log(layer)

        let targetLayerPath = targetLayerName == "SNOW-ON" ? "mar10idw" : "jul10idw";

        let renderMethodPath = renderMethod == "COLORRAMP" ? "png" : "hillshade";

        let demLayerUrl = tms_endpoint + renderMethodPath + '/' + targetLayerPath + '/{z}/{x}/{y}?colorRamp={colorRamp}'

        let targetLayer = [<TileLayer
                               key="targetLayer"
                               url={demLayerUrl}
                               colorRamp={colorRamp}
                               opacity={targetLayerOpacity}
                               maxZoom={19}
                           />]
        /* } else {
         *     console.log("HERE")
         *     var demLayerUrl = 'http://' + hostname + ':7070/tms/png/jul10idw/{z}/{x}/{y}?colorRamp={colorRamp}'
         *     targetLayer = [<TileLayer
         *                       key="targetLayer"
         *                       url={demLayerUrl}
         *                       colorRamp={colorRamp}
         *                       opacity={targetLayerOpacity}
         *                       maxZoom={19}
         *                   />]
         * }*/

        return (
            <LeafletMap
                center={[center.lat, center.lng]}
                zoom={zoom}
                ref={map => { this.map = map; }}
                animate
            >
                <TileLayer
                    url="https://stamen-tiles.a.ssl.fastly.net/terrain-background/{z}/{x}/{y}@2x.jpg"
                />

                <TileLayer
                    url="https://stamen-tiles.a.ssl.fastly.net/toner-labels/{z}/{x}/{y}@2x.png"
                />

              {targetLayer}
                {/* <TileLayer
                url={demLayerUrl}
                layer="mar10idw"
                colorRamp={colorRamp}
                opacity={targetLayerOpacity}
                maxZoom={19}
                />
                */}
            </LeafletMap>
        );
    }
}

Map.propTypes = {
    center: LocationTypeDef.isRequired,
    zoom: PropTypes.number.isRequired,
    targetLayerName: PropTypes.string.isRequired,
    renderMethod: PropTypes.string.isRequired,
};
