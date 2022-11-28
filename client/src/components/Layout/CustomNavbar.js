import React from 'react';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';

const CustomNavbar = ({ title, links }) => {
    const renderLinks = () => (
        links.map(link => 
            <Nav.Link href={`/${link.href}`}>{link.name}</Nav.Link>
        )
    );

    return (
        <Navbar bg="light" expand="lg">
            <Container>
                <Navbar.Brand href="#home">{ title }</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                <Nav className="me-auto">
                    {renderLinks()}
                </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
      );
};

export default CustomNavbar;