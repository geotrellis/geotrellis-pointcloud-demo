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
import Analysis from './Analysis';

import {
    setTargetLayerOpacity,
    setDataSourceType,
    clearGeometries,
    setPolygon,
    setPoint,
    setAnalysisOn,
    setActiveTab
} from './actions';

/* import PGWLogo from '../../img/geotrellis-logo.png';*/

class App extends Component {
    constructor(props) {
        super(props);

        this.onClearGeometries = this.onClearGeometries.bind(this);
        this.onSetPolygon = this.onSetPolygon.bind(this);
        this.onSetPoint = this.onSetPoint.bind(this);
        this.onAnalyzeClicked = this.onAnalyzeClicked.bind(this);
        this.onTabChanged = this.onTabChanged.bind(this);
    }

    onClearGeometries() {
        const { dispatch } = this.props;
        dispatch(clearGeometries());
    }

    onSetPolygon(polygon) {
        const { dispatch } = this.props;
        console.log("SET POLYGON: " + polygon);
        dispatch(setPolygon(polygon));
    }

    onSetPoint(point) {
        const { dispatch } = this.props;
        dispatch(setPoint(point));
    }

    onAnalyzeClicked() {
        const { dispatch } = this.props;
        dispatch(setAnalysisOn(true));
    }

    onTabChanged(selectedTabIndex, prevSelectedTabIndex) {
        const { dispatch } = this.props;
        dispatch(setActiveTab(selectedTabIndex));
    }

    render() {
        const {
            dispatch,
            activeTab,
            singleLayer,
            changeDetection,
            center,
            zoom,
            analysis,
        } = this.props;

        return (
            <div className="flex-expand-column height-100percent mode-detail pt-dark">
                <main>
                    <button className="button-analyze" onClick={this.onAnalyzeClicked}>Analyze</button>
                    <div className="sidebar options">
                        <Tabs
                            onChange={this.onTabChanged}
                            selectedTabIndex={activeTab}
                        >
                            <TabList className="main-tabs">
                                <Tab><span>Single Layer</span></Tab>
                                <Tab><span>Change Detection</span></Tab>
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

                    <Analysis
                        dispatch={dispatch}
                        analysisOn={analysis.analysisOn}
                        polygon={analysis.polygon}
                        point={analysis.point}
                        results={analysis.results}
                    />

                    <Map className="map"
                         center={center}
                         zoom={12}
                         singleLayer={singleLayer}
                         changeDetection={changeDetection}
                         analysisOn={analysis.analysisOn}
                         onClearGeometries={this.onClearGeometries}
                         onSetPolygon={this.onSetPolygon}
                         onSetPoint={this.onSetPoint}
                         polygon={analysis.polygon}
                         point={analysis.point}
                    />
                </main>
            </div>

        );
    }
}

function mapStateToProps({ appPage }) {
    return appPage;
}

export default connect(mapStateToProps)(App);
