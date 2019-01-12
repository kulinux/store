import React, { Component } from 'react';
import axios from 'axios';

import Small from '../small/Small';



import { withStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';


const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  paper: {
    padding: theme.spacing.unit * 2,
    textAlign: 'center',
    color: theme.palette.text.secondary,
  },
});



class List extends Component {


  constructor(props) {
    super(props);
    this.state = {
      products: []
    }
  }

  componentDidMount() {
    axios.get(this.props.url)
      .then(res => {
        const products = res.data;
        this.setState({ products });
      })
  }

  render() {
    const { classes } = this.props;

    return (
      <div className={classes.root}>
        <Grid container spacing={5}>
          {this.state.products.map(product => <Grid item xs>
              <Small {...product}/>
            </Grid>
          ) }
        </Grid>
      </div>
    )
  }
}

export default withStyles(styles)(List);
