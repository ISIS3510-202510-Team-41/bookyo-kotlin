package com.bookyo.home

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import com.bookyo.ui.BookyoTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.bookyo.components.BookyoButton
import com.bookyo.components.BottomNavigationBar
import com.bookyo.ui.*
import androidx.compose.runtime.*
import com.bookyo.auth.AmplifyAuthManager

class HomeScreenActivity: ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BookyoTheme {
                HomeScreen(viewModel)
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel) {

    // Hacer collect de la entidad como estado
    val books by viewModel.books.collectAsState()
    var selectedItem by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween // Espaciado entre elementos
    ) {
        // Toolbar (Título + Carrito)
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Home",
                style = typography.displayLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        // Contenedor de los espacios 1 y 2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Distribuye el espacio entre Toolbar y NavigationBar
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Espacio 1 - Imagen + Botón
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp, 170.dp)
                        .background(whiteGray)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(books.size) { index ->
                            books[index].title
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                BookyoButton(
                    onClick = { /* Acción para explorar libros */ },
                    modifier = Modifier.fillMaxWidth(0.8f), // Ajustar tamaño del botón
                    text = "Browse Books"
                )
            }

            Spacer(modifier = Modifier.height(40.dp)) // Espacio entre los elementos

            // Espacio 2 - Imagen + Botón
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp, 170.dp)
                        .background(whiteGray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BookyoButton(
                    onClick = { /* Acción para publicar un libro */ },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    text = "Publish Book"
                )
            }
        }

        // Barra de navegación inferior
        BottomNavigationBar(
            selectedItem = selectedItem,
            onItemSelected = { index ->
                {
                    selectedItem = index

                }
            }
        )
    }
}



