package tj.umar.navoplayer.feature.welcome.welcome

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoButton
import tj.umar.navoplayer.core.designsystem.component.Wordmark
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.MedallionVariant
import tj.umar.navoplayer.core.designsystem.medallion.rememberMedallionRotation
import tj.umar.navoplayer.core.designsystem.theme.NavoShadows
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.feature.welcome.R

private const val ENTER_DURATION_MILLIS = 450
private const val ENTER_STAGGER_MILLIS = 80

@Composable
internal fun WelcomeScreen(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        val minHeight = maxHeight
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .heightIn(min = minHeight)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(
                    start = NavoSpacing.OnboardingHorizontal,
                    top = 20.dp,
                    end = NavoSpacing.OnboardingHorizontal,
                    bottom = NavoSpacing.ExtraLarge,
                ),
        ) {
            StaggeredEnter(index = 0) {
                Box(modifier = Modifier.height(NavoSpacing.MinTouchTarget), contentAlignment = Alignment.CenterStart) {
                    Wordmark(small = true)
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            StaggeredEnter(index = 1) {
                MedallionComposition(modifier = Modifier.fillMaxWidth())
            }
            Spacer(modifier = Modifier.weight(1f))
            StaggeredEnter(index = 2) {
                Text(
                    text = stringResource(R.string.welcome_title),
                    style = typography.displayL,
                    color = colors.content,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            StaggeredEnter(index = 3) {
                Column {
                    Text(
                        text = stringResource(R.string.welcome_body),
                        style = typography.body,
                        color = colors.contentMuted,
                    )
                    PermissionHint(permission = state.permission)
                }
            }
            Spacer(modifier = Modifier.height(NavoSpacing.ExtraLarge))
            StaggeredEnter(index = 4) {
                GrantButton(
                    permanentlyDenied = state.permission == WelcomePermissionStatus.PermanentlyDenied,
                    onClick = { onIntent(WelcomeIntent.GrantAccessClicked) },
                )
            }
        }
    }
}

@Composable
private fun PermissionHint(permission: WelcomePermissionStatus) {
    AnimatedVisibility(
        visible = permission != WelcomePermissionStatus.NotRequested,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val hint = if (permission == WelcomePermissionStatus.PermanentlyDenied) {
            R.string.welcome_permanently_denied_hint
        } else {
            R.string.welcome_denied_hint
        }
        Text(
            text = stringResource(hint),
            style = NavoTheme.typography.caption,
            color = NavoTheme.colors.accent,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun GrantButton(permanentlyDenied: Boolean, onClick: () -> Unit) {
    AnimatedContent(
        targetState = permanentlyDenied,
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
        label = "grantButton",
    ) { openSettings ->
        val label = if (openSettings) R.string.welcome_open_settings else R.string.welcome_grant_access
        NavoButton(
            text = stringResource(label),
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MedallionComposition(modifier: Modifier = Modifier) {
    val background = NavoTheme.colors.background
    val palettes = MedallionPalettes.all
    Box(
        modifier = modifier
            .height(320.dp)
            .clearAndSetSemantics { },
    ) {
        Box(
            modifier = Modifier
                .offset(x = 4.dp, y = 30.dp)
                .size(220.dp)
                .dropShadow(CircleShape, NavoShadows.Medallion),
        ) {
            Medallion(
                palette = palettes[0],
                modifier = Modifier.fillMaxSize(),
                rotationDegrees = rememberMedallionRotation(running = true),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-6).dp)
                .size(144.dp)
                .background(background, CircleShape)
                .padding(6.dp),
        ) {
            Medallion(
                palette = palettes[1],
                variant = MedallionVariant.Simple,
                modifier = Modifier.fillMaxSize(),
                rotationDegrees = rememberMedallionRotation(running = true, periodMillis = 60_000, clockwise = false),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-28).dp, y = 190.dp)
                .size(116.dp)
                .background(background, CircleShape)
                .padding(6.dp),
        ) {
            Medallion(
                palette = palettes[3],
                modifier = Modifier.fillMaxSize(),
                variant = MedallionVariant.Simple,
            )
        }
    }
}


@Composable
private fun StaggeredEnter(index: Int, content: @Composable () -> Unit) {
    val inPreview = LocalInspectionMode.current
    val visibleState = remember { MutableTransitionState(inPreview).apply { targetState = true } }
    val delay = ENTER_STAGGER_MILLIS * index
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(ENTER_DURATION_MILLIS, delayMillis = delay)) +
            slideInVertically(tween(ENTER_DURATION_MILLIS, delayMillis = delay)) { it / 6 },
    ) {
        content()
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun WelcomeScreenPreview() {
    NavoTheme {
        WelcomeScreen(state = WelcomeState(), onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun WelcomeScreenPermanentlyDeniedPreview() {
    NavoTheme {
        WelcomeScreen(
            state = WelcomeState(permission = WelcomePermissionStatus.PermanentlyDenied),
            onIntent = {},
        )
    }
}
