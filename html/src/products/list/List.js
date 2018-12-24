import React, { Component } from 'react';
import axios from 'axios';

import Small from '../small/Small';

import './List.css'

export class List extends Component {

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
    return (
      <div className="List">
        {this.state.products.map(product => <div className="List-item">
          <Small {...product}/></div>
        ) }
      </div>

    )
  }
}

export default List;
