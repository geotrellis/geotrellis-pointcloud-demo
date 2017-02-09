import React, { Component, PropTypes } from 'react';

import { Slider, Button, Tabs, TabList, TabPanel, Tab } from "@blueprintjs/core";

import {
    setTargetLayerOpacity,
    setDataSourceType,
    setTargetLayerName,
    setRenderMethod
} from './actions';

export default class SingleLayer extends Component {
    constructor() {
        super();

        this.handleTargetLayerOpacityChange = this.handleTargetLayerOpacityChange.bind(this);
        this.checkStatic = this.checkStatic.bind(this);
        this.checkDynamic = this.checkDynamic.bind(this);
        this.checkSnowOn = this.checkSnowOn.bind(this);
        this.checkSnowOff = this.checkSnowOff.bind(this);
        this.checkColorRamp = this.checkColorRamp.bind(this);
        this.checkHillshade = this.checkHillshade.bind(this);
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

    checkSnowOn() {
        const { dispatch } = this.props;
        dispatch(setTargetLayerName("SNOW-ON"));
    }

    checkSnowOff() {
        const { dispatch } = this.props;
        dispatch(setTargetLayerName("SNOW-OFF"));
    }

    checkColorRamp() {
        const { dispatch } = this.props;
        dispatch(setRenderMethod("COLORRAMP"));
    }

    checkHillshade() {
        const { dispatch } = this.props;
        dispatch(setRenderMethod("HILLSHADE"));
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
            colorRampChecked,
            hillshadeChecked,
            snowOnChecked,
            snowOffChecked } = this.props;

        return (
            <div className="content tab-content content-singlelayer active">
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
                    <div class="pt-button-group pt-large pt-fill">
                        <Button
                            active={staticChecked}
                            onClick={this.checkStatic}
                            text="TIN"
                        />
                        <Button
                            active={dynamicChecked}
                            onClick={this.checkDynamic}
                            text="IDW"
                        />
                    </div>
                </div>
                <div className="option-section">
                    <label htmlFor="" className="primary">Render Options</label>
                    <div class="pt-button-group pt-large pt-fill">
                        <Button
                            active={colorRampChecked}
                            onClick={this.checkColorRamp}
                            text="Color Ramp"
                        />
                        <Button
                            active={hillshadeChecked}
                            onClick={this.checkHillshade}
                            text="Hillshade"
                        />
                    </div>
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
                <div className="option-section">
                    <label htmlFor="" className="primary">Dataset</label>
                    <div class="pt-button-group pt-large pt-fill">
                        <Button
                            active={snowOnChecked}
                            onClick={this.checkSnowOn}
                            text="SNOW ON"
                        />
                        <Button
                            active={snowOffChecked}
                            onClick={this.checkSnowOff}
                            text="SNOW OFF"
                        />
                    </div>
                </div>
            </div>
        );
    }
}

SingleLayer.propTypes = {
    dispatch: PropTypes.func.isRequired,
    idwChecked: PropTypes.bool.isRequired,
    tinChecked: PropTypes.bool.isRequired,
    staticChecked: PropTypes.bool.isRequired,
    dynamicChecked: PropTypes.bool.isRequired,
    targetLayerOpacity: PropTypes.number.isRequired,
    colorRampChecked: PropTypes.bool.isRequired,
    hillshadeChecked: PropTypes.bool.isRequired,
    snowOnChecked: PropTypes.bool.isRequired,
    snowOffChecked: PropTypes.bool.isRequired
}
