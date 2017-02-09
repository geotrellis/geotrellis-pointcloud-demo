import React, { Component, PropTypes } from 'react';

import { Slider, Button, Tabs, TabList, TabPanel, Tab } from "@blueprintjs/core";

import {
    setTargetLayerOpacity,
    setDataSourceType
} from './actions';

export default class ChangeDetection extends Component {
    constructor() {
        super();

        this.handleTargetLayerOpacityChange = this.handleTargetLayerOpacityChange.bind(this);
        this.checkStatic = this.checkStatic.bind(this);
        this.checkDynamic = this.checkDynamic.bind(this);
    }

    handleTargetLayerOpacityChange(value) {
        const { dispatch } = this.props;
        console.log("CHANGING");
        dispatch(setTargetLayerOpacity(value));
    }

    checkStatic() {
        const { dispatch } = this.props;
        dispatch(setDataSourceType("DYNAMIC"));
    }

    checkDynamic() {
        const { dispatch } = this.props;
        dispatch(setDataSourceType("STATIC"));
    }

    /* checkIdw() {
     *     dispatch(setDEMAlogrithm("IDW"));
     * }

     * checkTin() {
     *     dispatch(setDEMAlgorithm("TIN"));
     * }*/

    render() {
        const {
            idwChecked,
            tinChecked,
            staticChecked,
            dynamicChecked,
            targetLayerOpacity,
        } = this.props;

        return (
            <div className="content tab-content content-changedetection active">
                <div className="option-section">
                    <label htmlFor="" className="primary">Data Source Type</label>
                    <div class="pt-button-group pt-large pt-fill">
                        <Button
                            active={staticChecked}
                            onClick={this.checkStatic}
                            text="STATIC"
                        />
                        <Button
                            active={dynamicChecked}
                            onClick={this.checkDynamic}
                            text="DYNAMIC"
                        />
                    </div>
                    <label htmlFor="" className="secondary">Min &amp; Max Elevation</label>
                    <div>(Slider)</div>
                </div>
                <div className="option-section">
                    <label htmlFor="" className="primary">DEM Creation Method</label>
                    <div>(Tab: TIN, DEM)</div>
                </div>
                <div className="option-section">
                    <label htmlFor="" className="primary">Render Options</label>
                    <label htmlFor="" className="secondary">Opacity</label>
                    <div>
                        <Slider
                            min={0}
                            max={1}
                            stepSize={0.02}
                            renderLabel={false}
                            value={targetLayerOpacity}
                            onChange={this.handleTargetLayerOpacityChange}
                        />
                    </div>
                </div>
            </div>
        );
    }
}

ChangeDetection.propTypes = {
    dispatch: PropTypes.func.isRequired,
    idwChecked: PropTypes.bool.isRequired,
    tinChecked: PropTypes.bool.isRequired,
    staticChecked: PropTypes.bool.isRequired,
    dynamicChecked: PropTypes.bool.isRequired,
    targetLayerOpacity: PropTypes.number.isRequired,
}
