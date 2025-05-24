package com.bookyo.searchFeed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amplifyframework.datastore.generated.model.Book
import com.bookyo.components.BookThumbnail
import com.bookyo.searchFeed.AuthorUIModel
import com.bookyo.searchFeed.BookUIModel
import com.bookyo.searchFeed.ListingUIModel
import com.bookyo.ui.white

/**
 * A reusable book card component to display book details.
 * Can optionally display listing information if the book is for sale.
 *
 * @param book The book UI model containing details to display
 * @param listing The optional listing UI model containing seller and price info
 * @param onClick Callback triggered when the card is clicked
 */
@Composable
fun BookCard(
    book: BookUIModel,
    listing: ListingUIModel? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = white
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(8.dp)
        ) {
            // Book thumbnail
            BookThumbnail(
                thumbnailKey = book.thumbnail,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
            )

            // Book details
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 12.dp, top = 4.dp, end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Book title
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    /* Author name
                    Text(
                        text = "by ${book.author.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )*/

                    Spacer(modifier = Modifier.height(4.dp))

                    // ISBN
                    Text(
                        text = "ISBN: ${book.isbn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Show listing information if available
                if (listing != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Seller info
                        Text(
                            text = "Seller: ${listing.seller}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Price
                        // The price is a Float in the model
                        Text(
                            text = "${String.format("%.2f", listing.price)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}