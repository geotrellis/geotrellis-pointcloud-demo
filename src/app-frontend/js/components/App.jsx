import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import { browserHistory } from 'react-router';
import { head, isEqual, isEmpty } from 'lodash';
import { Slider, Button, Tabs, TabList, TabPanel, Tab } from "@blueprintjs/core";

import ScrollButton from 'components/ScrollButton';
import {
    GeoJSONGeometryTypeDef,
    QueryResultTypeDef,
} from 'TypeDefs';

import Map from './Map';
import SingleLayer from './SingleLayer';
import ChangeDetection from './ChangeDetection';

import {
    setTargetLayerOpacity,
    setDataSourceType
} from './actions';

/* import PGWLogo from '../../img/geotrellis-logo.png';*/

class App extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const {
            dispatch,
            singleLayer,
            changeDetection,
            center,
            zoom
        } = this.props;

        return (
            <div className="flex-expand-column height-100percent mode-detail">
                <main>
                    <button className="button-analyze">Analyze</button>
                    <div className="sidebar options">
                        <Tabs>
                            <TabList className="main-tabs">
                                <Tab>Single Layer</Tab>
                                <Tab>Change Detection</Tab>
                            </TabList>
                            <TabPanel>
                                <SingleLayer
                                    dispatch={dispatch}
                                    idwChecked={singleLayer.idwChecked}
                                    tinChecked={singleLayer.tinChecked}
                                    staticChecked={singleLayer.staticChecked}
                                    dynamicChecked={singleLayer.dynamicChecked}
                                    targetLayerOpacity={singleLayer.targetLayerOpacity}
                                    colorRampChecked={singleLayer.colorRampChecked}
                                    hillshadeChecked={singleLayer.hillshadeChecked}
                                    snowOnChecked={singleLayer.snowOnChecked}
                                    snowOffChecked={singleLayer.snowOffChecked}
                                />
                            </TabPanel>
                            <TabPanel>
                                <ChangeDetection
                                    dispatch={dispatch}
                                    idwChecked={changeDetection.idwChecked}
                                    tinChecked={changeDetection.tinChecked}
                                    staticChecked={changeDetection.staticChecked}
                                    dynamicChecked={changeDetection.dynamicChecked}
                                    targetLayerOpacity={changeDetection.targetLayerOpacity}
                                />

                            </TabPanel>
                        </Tabs>
                    </div>

                    <div className="sidebar analyze active">
                        <header>
                            <div className="sidebar-heading">Analyze</div>
                            <button className="button-cancel">
                                <i className="icon icon-cancel"></i>
                            </button>
                        </header>
                        <div className="draw-buttons">
                            <div className="button-group">
                                <button className="button active">Draw</button>
                                <button className="button">Point</button>
                            </div>
                        </div>
                        <div className="content">
                            <div className="analyze-description active">
                                <p>Click on the "Draw" button to draw a polygon and
                                    calculate statistics over that area.</p>
                                <p>Click on the "Point" button to select a point
                                    on the map and see statistics for that
                                    location.</p>

                            </div>
                            <div className="analyze-result-wrapper active">
                                <div className="analyze-result">
                                    <div className="analyze-number">7.2</div>
                                    <div className="label primary" htmlFor="">Average Elevation (ft)</div>
                                </div>
                                <div className="analyze-result">
                                    <div className="analyze-number">6.5</div>
                                    <div className="label primary" htmlFor="">Minimum Elevation (ft)</div>
                                </div>
                                <div className="analyze-result">
                                    <div className="analyze-number">9.6</div>
                                    <div className="label primary" htmlFor="">Maximum Elevation (ft)</div>
                                </div>
                                <div className="analyze-result">
                                    <div className="analyze-number">10.2</div>
                                    <div className="label primary" htmlFor="">Volume Change (sqft)</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <Map className="map"
                         targetLayerOpacity={singleLayer.targetLayerOpacity}
                         center={center}
                         zoom={11}
                         targetLayerName={singleLayer.targetLayerName}
                         renderMethod={singleLayer.renderMethod}
                    />
                </main>
            </div>

        );
    }
}

App.propTypes = {
    dispatch: PropTypes.func.isRequired,
    address: PropTypes.object.isRequired,
    neighborhoods: QueryResultTypeDef,
    communityOrgs: QueryResultTypeDef,
    policeDistricts: QueryResultTypeDef,
    fireStations: QueryResultTypeDef,
    nec: QueryResultTypeDef,
    nac: QueryResultTypeDef,
    radius: PropTypes.number.isRequired,
    politics: QueryResultTypeDef,
    censusTracts: QueryResultTypeDef,
    demographics: QueryResultTypeDef,
    hoverGeom: GeoJSONGeometryTypeDef,
    highlightedGeom: GeoJSONGeometryTypeDef,
    highlightedCard: PropTypes.string,
};

function mapStateToProps({ appPage }) {
    return appPage;
}

export default connect(mapStateToProps)(App);
