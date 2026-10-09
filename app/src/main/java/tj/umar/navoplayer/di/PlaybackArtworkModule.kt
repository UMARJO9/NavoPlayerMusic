package tj.umar.navoplayer.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.renderPng
import tj.umar.navoplayer.core.player.artwork.ResumptionArtwork
import javax.inject.Inject

internal class MedallionResumptionArtwork @Inject constructor() : ResumptionArtwork {
    override fun render(trackId: Long): ByteArray = MedallionPalettes.forKey(trackId).renderPng()
}

@Module
@InstallIn(SingletonComponent::class)
internal interface PlaybackArtworkModule {

    @Binds
    fun bindsResumptionArtwork(artwork: MedallionResumptionArtwork): ResumptionArtwork
}
